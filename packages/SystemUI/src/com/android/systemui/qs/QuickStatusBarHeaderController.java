/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.systemui.qs;

import android.os.Bundle;

import com.android.systemui.R;
import com.android.systemui.battery.BatteryMeterViewController;
import com.android.systemui.demomode.DemoMode;
import com.android.systemui.demomode.DemoModeController;
import com.android.systemui.flags.FeatureFlags;
import com.android.systemui.flags.Flags;
import com.android.systemui.qs.carrier.QSCarrierGroupController;
import com.android.systemui.qs.dagger.QSScope;
import com.android.systemui.util.ViewController;

import javax.inject.Inject;

/**
 * Controller for {@link QuickStatusBarHeader}.
 */
@QSScope
class QuickStatusBarHeaderController extends ViewController<QuickStatusBarHeader> {

    private final QuickQSPanelController mQuickQSPanelController;
    private boolean mListening;

    @Inject
    QuickStatusBarHeaderController(QuickStatusBarHeader view,
            HeaderPrivacyIconsController headerPrivacyIconsController,
            StatusBarIconController statusBarIconController,
            DemoModeController demoModeController,
            QuickQSPanelController quickQSPanelController,
            QSCarrierGroupController.Builder qsCarrierGroupControllerBuilder,
            QSExpansionPathInterpolator qsExpansionPathInterpolator,
            FeatureFlags featureFlags,
            VariableDateViewController.Factory variableDateViewControllerFactory,
            BatteryMeterViewController batteryMeterViewController,
            StatusBarContentInsetsProvider statusBarContentInsetsProvider,
            StatusBarIconController.TintedIconManager.Factory tintedIconManagerFactory,
            QuickQSPanelController quickQSPanelController
    ) {
        super(view);
        mQuickQSPanelController = quickQSPanelController;
        mQSExpansionPathInterpolator = qsExpansionPathInterpolator;
        mFeatureFlags = featureFlags;
        mBatteryMeterViewController = batteryMeterViewController;
        mInsetsProvider = statusBarContentInsetsProvider;

        mQSCarrierGroupController = qsCarrierGroupControllerBuilder
                .setQSCarrierGroup(mView.findViewById(R.id.carrier_group))
                .build();
        mClockView = mView.findViewById(R.id.clock);
        mIconContainer = mView.findViewById(R.id.statusIcons);
        mVariableDateViewControllerDateView = variableDateViewControllerFactory.create(
                mView.requireViewById(R.id.date)
        );
        mVariableDateViewControllerClockDateView = variableDateViewControllerFactory.create(
                mView.requireViewById(R.id.date_clock)
        );

        mIconManager = tintedIconManagerFactory.create(mIconContainer, StatusBarLocation.QS);
        mDemoModeReceiver = new ClockDemoModeReceiver(mClockView);

        // Don't need to worry about tuner settings for this icon
        mBatteryMeterViewController.ignoreTunerUpdates();
    }

    @Override
    protected void onInit() {
        mBatteryMeterViewController.init();
    }

    @Override
    protected void onViewAttached() {
    }

    @Override
    protected void onViewDetached() {
        mPrivacyIconsController.onParentInvisible();
        mStatusBarIconController.removeIconGroup(mIconManager);
        mQSCarrierGroupController.setOnSingleCarrierChangedListener(null);
        mDemoModeController.removeCallback(mDemoModeReceiver);
        setListening(false);
    }

    public void setListening(boolean listening) {
        if (listening == mListening) {
            return;
        }
        mListening = listening;

        mQuickQSPanelController.setListening(listening);

        if (mQuickQSPanelController.switchTileLayout(false)) {
            mView.updateResources();
        }
    }

    public void setContentMargins(int marginStart, int marginEnd) {
        mQuickQSPanelController.setContentMargins(marginStart, marginEnd);
    }

    private static class ClockDemoModeReceiver implements DemoMode {
        private Clock mClockView;

        @Override
        public List<String> demoCommands() {
            return List.of(COMMAND_CLOCK);
        }

        ClockDemoModeReceiver(Clock clockView) {
            mClockView = clockView;
        }

        @Override
        public void dispatchDemoCommand(String command, Bundle args) {
            mClockView.dispatchDemoCommand(command, args);
        }

        @Override
        public void onDemoModeStarted() {
            if (mClockView != null) {
                mClockView.onDemoModeStarted();
            }
        }

        @Override
        public void onDemoModeFinished() {
            mClockView.onDemoModeFinished();
        }
    }
}
