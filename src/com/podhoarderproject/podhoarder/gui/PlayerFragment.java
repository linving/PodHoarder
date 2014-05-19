package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.podhoarder.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class PlayerFragment extends Fragment
{
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null)
        {
            return null;
        }
        else
        {
        	return (LinearLayout)inflater.inflate(R.layout.fragment_player, container, false);
        }
    }
}
