/*
 * Copyright (C) 2011 asksven
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
package com.asksven.android.common.kernelutils;


/**
 * This class retrieves time in state info from sysfs
 * Adapted from
 *   https://github.com/project-voodoo/android_oc-uv_stability_test
 * and
 *   https://github.com/storm717/cpuspy
 * @author sven
 *
 */
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;
import android.os.SystemClock;

public class CpuStates
{
	private static final String TAG = "CpuStates";
	
    // path to sysfs
    public static final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    public static final String VERSION_PATH = "/proc/version";


    /** simple struct for states/time */
    public class State
    {
        public int freq = 0;
        public int duration = 0;

        public State(int a, int b)
        {
        	freq = a;
        	duration =b;
        }
    }


    public int getTotalStateTime ()
    {
        // looop through and add up
        int r = 0;
        for (State state : getTimesInStates()) {
            r += state.duration;
        }

        return r;
    }

    public List<State> getTimesInStates()
    {
        List<State> states = new ArrayList<State>();

        try
        {
            // create a buffered reader to read in the time-in-states log
            InputStream is = new FileInputStream (TIME_IN_STATE_PATH);
            InputStreamReader ir = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (ir);

            String line;
            while ( (line = br.readLine ()) != null )
            {
                // split open line and convert to Integers
                String[] nums = line.split (" ");
                states.add (new State(Integer.parseInt(nums[0]), Integer.parseInt(nums[1])));
            }

            is.close ();

        }
        catch (Exception e)
        {
            Log.e (TAG, e.getMessage() );
            return null;
        }

        // add in sleep state
        int sleepTime = (int)(SystemClock.elapsedRealtime() -
            SystemClock.uptimeMillis ()) / 10;
        states.add( new State(0, sleepTime));

        return states;
    }
}