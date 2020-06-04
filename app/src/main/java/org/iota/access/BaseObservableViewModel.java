/*
 *  This file is part of the IOTA Access distribution
 *  (https://github.com/iotaledger/access)
 *
 *  Copyright (c) 2020 IOTA Stiftung.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.iota.access;

import androidx.lifecycle.ViewModel;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.annotation.NonNull;

public class BaseObservableViewModel extends ViewModel implements Observable {
        private transient PropertyChangeRegistry mCallbacks;

        public BaseObservableViewModel() {
        }

        @Override
        public void addOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
            synchronized (this) {
                if (mCallbacks == null) {
                    mCallbacks = new PropertyChangeRegistry();
                }
            }
            mCallbacks.add(callback);
        }

        @Override
        public void removeOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
            synchronized (this) {
                if (mCallbacks == null) {
                    return;
                }
            }
            mCallbacks.remove(callback);
        }

        /**
         * Notifies listeners that all properties of this instance have changed.
         */
        public void notifyChange() {
            synchronized (this) {
                if (mCallbacks == null) {
                    return;
                }
            }
            mCallbacks.notifyCallbacks(this, 0, null);
        }

        /**
         * Notifies listeners that a specific property has changed. The getter for the property
         * that changes should be marked with {@link Bindable} to generate a field in
         * <code>BR</code> to be used as <code>fieldId</code>.
         *
         * @param fieldId The generated BR id for the Bindable field.
         */
        public void notifyPropertyChanged(int fieldId) {
            synchronized (this) {
                if (mCallbacks == null) {
                    return;
                }
            }
            mCallbacks.notifyCallbacks(this, fieldId, null);
        }
}
