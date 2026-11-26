/**
 * Copyright (C) 2015 Sebastian Kappes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tech.alatcerdas.tvkillindo;

import android.content.Context;

import java.util.Arrays;


public class Pattern {

    private int[] pattern;
    private int frequency;
    private boolean converted = false;

    //This method converts the remotes pattern, if this has not been done yet, and passes the converted pattern to the initiate-method.
    public void send(Context c) {

        if (!converted) {
            pattern = convert(pattern, frequency);
            converted = true;
        }
        initiate(c);

    }

    //This method initiates the remote's transmission.
    private void initiate(Context c) {
        Transmitter remote = new Transmitter(frequency, pattern);
        remote.transmit(c);
    }


    protected Pattern(int frequency, int[] pattern) {
        this.frequency = frequency;
        this.pattern = pattern;
    }

    protected Pattern(int[] ircode) {
        this.frequency = ircode[0];
        this.pattern = Arrays.copyOfRange(ircode, 1, ircode.length);
    }

    // public accessors so unit tests can verify the catalog data
    // (issues #15/#16/#18) without changing emission behavior
    public int rawFrequency() {
        return frequency;
    }

    public int[] rawPattern() {
        return pattern;
    }


    /*
     * The following method performs calculations on the IR-codes in order to make them
     * work on supported devices. Since minSdk is 24, every device uses the normal
     * API >= 21 conversion path that was previously selected for Lollipop and newer
     * (including HTC devices). The pre-Lollipop / Samsung-specific fallbacks are gone.
     */
    private static int[] convert(int[] irData, int frequency) {
        for (int i = 0; i < irData.length; i++) {
            irData[i] = irData[i] * (1000000 / frequency);
        }
        return irData;
    }
}
