package com.podhoarderproject.podhoarder.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.widget.PopupMenu;

public class PopupMenuUtils
{
	public static void forceShowIcons(PopupMenu popup)
	{
		try 
		{
		    Field[] fields = popup.getClass().getDeclaredFields();
		    for (Field field : fields) 
		    {
		        if ("mPopup".equals(field.getName())) 
		        {
		            field.setAccessible(true);
		            Object menuPopupHelper = field.get(popup);
		            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
		            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
		            setForceIcons.invoke(menuPopupHelper, true);
		            break;
		        }
		    }
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
		}
	}
}
