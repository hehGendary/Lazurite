package com.kingmang.lazurite.libraries.GForms;


import com.kingmang.lazurite.core.*;
import com.kingmang.lazurite.runtime.LZR.LZRMap;
import com.kingmang.lazurite.runtime.LZR.LZRNumber;
import com.kingmang.lazurite.runtime.LZR.LZRString;
import com.kingmang.lazurite.runtime.Value;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowValue extends ContainerValue {

    private final Window window;

    public WindowValue(int functionsCount, Window window) {
        super(functionsCount + 18, window);
        this.window = window;
        init();
    }

    private void init() {
        set("addWindowListener", this::addWindowListener);
        set("dispose", Converters.voidToVoid(window::dispose));
        set("isActive", Converters.voidToBoolean(window::isActive));
        set("isAlwaysOnTop", Converters.voidToBoolean(window::isAlwaysOnTop));
        set("isAlwaysOnTopSupported", Converters.voidToBoolean(window::isAlwaysOnTopSupported));
        set("isAutoRequestFocus", Converters.voidToBoolean(window::isAutoRequestFocus));
        set("isFocusableWindow", Converters.voidToBoolean(window::isFocusableWindow));
        set("isFocused", Converters.voidToBoolean(window::isFocused));
        set("isLocationByPlatform", Converters.voidToBoolean(window::isLocationByPlatform));
        set("isShowing", Converters.voidToBoolean(window::isShowing));
        set("getOpacity", Converters.voidToFloat(window::getOpacity));
        set("pack", Converters.voidToVoid(window::pack));
        set("setAlwaysOnTop", Converters.booleanOptToVoid(window::setAlwaysOnTop));
        set("setAutoRequestFocus", Converters.booleanToVoid(window::setAutoRequestFocus));
        set("setFocusableWindowState", Converters.booleanToVoid(window::setFocusableWindowState));
        set("setLocationByPlatform", Converters.booleanOptToVoid(window::setLocationByPlatform));
        set("setOpacity", Converters.floatToVoid(window::setOpacity));
        set("toBack", Converters.voidToVoid(window::toBack));
        set("toFront", Converters.voidToVoid(window::toFront));
    }
    
    
    private Value addWindowListener(Value[] args) {
        Arguments.check(1, args.length);
        final Function action = ValueUtils.consumeFunction(args[0], 0);
        window.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                handleWindowEvent("opened", e);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowEvent("closing", e);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                handleWindowEvent("closed", e);
            }

            @Override
            public void windowIconified(WindowEvent e) {
                handleWindowEvent("iconified", e);
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                handleWindowEvent("deiconified", e);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                handleWindowEvent("activated", e);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                handleWindowEvent("deactivated", e);
            }
            
            private void handleWindowEvent(String type, final WindowEvent e) {
                final LZRMap map = new LZRMap(4);
                map.set("id", LZRNumber.of(e.getID()));
                map.set("newState", LZRNumber.of(e.getNewState()));
                map.set("oldState", LZRNumber.of(e.getOldState()));
                map.set("paramString", new LZRString(e.paramString()));
                action.execute(new LZRString(type), map);
            }
        });
        return LZRNumber.ZERO;
    }
}