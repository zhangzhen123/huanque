/*
 * Copyright (C) 2015-2017 Jacksgong(blog.dreamtobe.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.julun.huanque.common.widgets.kpswitch;

import com.julun.huanque.common.widgets.kpswitch.handler.KPSwitchPanelLayoutHandler;
import com.julun.huanque.common.widgets.kpswitch.util.KPSwitchConflictUtil;
import com.julun.huanque.common.widgets.kpswitch.util.KeyboardUtil;

/**
 * Created by Jacksgong on 3/30/16.
 * <p>
 * The interface used for the panel's container layout and it used in the case of non-full-screen
 * theme window.
 */
public interface IPanelConflictLayout {
    boolean isKeyboardShowing();

    /**
     * @return The real status of Visible or not
     */
    boolean isVisible();

    /**
     * Keyboard->Panel
     *
     * @see KPSwitchConflictUtil#showPanel(android.view.View)
     */
    void handleShow();

    /**
     * Panel->Keyboard
     *
     * @see KPSwitchConflictUtil#showKeyboard
     */
    void handleHide();

    /**
     * @param isIgnoreRecommendHeight Ignore guaranteeing the panel height equal to the keyboard
     *                                height.
     * @attr ref com.julun.lingmeng.lmcore.kpswitch.R.styleable#KPSwitchPanelLayout_ignore_recommend_height
     * @see KPSwitchPanelLayoutHandler#resetToRecommendPanelHeight
     * @see KeyboardUtil#getValidPanelHeight(android.content.Context)
     */
    @SuppressWarnings("JavaDoc")
    void setIgnoreRecommendHeight(boolean isIgnoreRecommendHeight);
}
