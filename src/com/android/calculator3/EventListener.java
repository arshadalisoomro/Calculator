/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator3;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

class EventListener implements View.OnKeyListener,
                               View.OnClickListener,
                               View.OnLongClickListener {
    Context mContext;
    Logic mHandler;
    ViewPager mPager;

    void setHandler(Context context, Logic handler, ViewPager pager) {
        mContext = context;
        mHandler = handler;
        mPager = pager;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
        case R.id.del:
            mHandler.onDelete();
            break;

        case R.id.clear:
            mHandler.onClear();
            break;

        case R.id.equal:
            if (mHandler.getText().contains(mContext.getResources().getString(R.string.X)) || 
                mHandler.getText().contains(mContext.getResources().getString(R.string.Y))) {
                if (!mHandler.getText().contains("=")) {
                    mHandler.insert("=");
                }
                break;
            }
            mHandler.onEnter();
            break;

        default:
            if (view instanceof Button) {
                String text = ((Button) view).getText().toString();
                if(text.equals("( )")){
                    if(mHandler.getText().contains("=")){
                        text = mHandler.getText().split("=")[0] + "=(" + mHandler.getText().split("=")[1] + ")";
                    }
                    else{
                        text = "(" + mHandler.getText() + ")";
                    }
                    mHandler.clear(false);
                }
                else if(text.equals(mContext.getResources().getString(R.string.mod))){
                	if(mHandler.getText().contains("=")){
                        if(mHandler.getText().split("=").length>1){
                            text = mHandler.getText().split("=")[0] + "=" + mContext.getResources().getString(R.string.mod)+"("+mHandler.getText().split("=")[1]+",";
                            mHandler.clear(false);
                        }
                        else{
                            text = mContext.getResources().getString(R.string.mod)+"(";
                        }
                    }
                    else{
                    	if(mHandler.getText().length()>0){
                            text = mContext.getResources().getString(R.string.mod)+"("+mHandler.getText()+",";
                            mHandler.clear(false);
                        }
                        else{
                            text = mContext.getResources().getString(R.string.mod)+"(";
                        }
                    }
                }
                else if(text.equals(mContext.getResources().getString(R.string.eigenvalue))){
                	mHandler.findEigenvalue();
                	return;
                }
                else if(text.equals(mContext.getResources().getString(R.string.determinant))){
                	mHandler.findDeterminant();
                	return;
                }
                else if(text.equals(mContext.getResources().getString(R.string.solve))){
                	mHandler.solveMatrix();
                	return;
                }
                else if(text.equals(mContext.getResources().getString(R.string.solveForX)) || text.equals(mContext.getResources().getString(R.string.solveForY)) || (text.equals(mContext.getResources().getString(R.string.dx))) || (text.equals(mContext.getResources().getString(R.string.dy)))){
                    //Do nothing
                }
                else if (text.length() >= 2) {
                    // add paren after sin, cos, ln, etc. from buttons
                    text += '(';
                }
                mHandler.insert(text);
                if (mPager != null && (mPager.getCurrentItem() == Calculator.ADVANCED_PANEL || mPager.getCurrentItem() == Calculator.FUNCTION_PANEL)) {
                    mPager.setCurrentItem(Calculator.BASIC_PANEL);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (id == R.id.del) {
            mHandler.onClear();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        int action = keyEvent.getAction();

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            boolean eat = mHandler.eatHorizontalMove(keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
            return eat;
        }

        //Work-around for spurious key event from IME, bug #1639445
        if (action == KeyEvent.ACTION_MULTIPLE && keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            return true; // eat it
        }
        
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (mHandler.getText().endsWith(mContext.getResources().getString(R.string.sin) + "(") || 
                mHandler.getText().endsWith(mContext.getResources().getString(R.string.cos) + "(") ||
                mHandler.getText().endsWith(mContext.getResources().getString(R.string.tan) + "(") ||
                mHandler.getText().endsWith(mContext.getResources().getString(R.string.lg) + "(") || 
                mHandler.getText().endsWith(mContext.getResources().getString(R.string.mod) + "(")){
                String text = mHandler.getText().substring(0, mHandler.getText().length()-4);
                mHandler.clear(false);
                mHandler.insert(text);
                return true;
            }
            else if (mHandler.getText().endsWith(mContext.getResources().getString(R.string.ln) + "(")){
                String text = mHandler.getText().substring(0, mHandler.getText().length()-3);
                mHandler.clear(false);
                mHandler.insert(text);
            }
            else if (mHandler.getText().endsWith(mContext.getResources().getString(R.string.dx)) ||
                     mHandler.getText().endsWith(mContext.getResources().getString(R.string.dy))){
                String text = mHandler.getText().substring(0, mHandler.getText().length()-2);
                mHandler.clear(false);
                mHandler.insert(text);
                return true;
            }
            return false;
        }

        //Calculator.log("KEY " + keyCode + "; " + action);

        if (keyEvent.getUnicodeChar() == '=') {
            if (action == KeyEvent.ACTION_UP) {
                mHandler.onEnter();
            }
            return true;
        }

        if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
            keyCode != KeyEvent.KEYCODE_DPAD_UP &&
            keyCode != KeyEvent.KEYCODE_DPAD_DOWN &&
            keyCode != KeyEvent.KEYCODE_ENTER) {
            if (keyEvent.isPrintingKey() && action == KeyEvent.ACTION_UP) {
                // Tell the handler that text was updated.
                mHandler.onTextChanged();
            }
            return false;
        }

        /*
           We should act on KeyEvent.ACTION_DOWN, but strangely
           sometimes the DOWN event isn't received, only the UP.
           So the workaround is to act on UP...
           http://b/issue?id=1022478
         */

        if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mHandler.onEnter();
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                mHandler.onUp();
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                mHandler.onDown();
                break;
            }
        }
        return true;
    }
}