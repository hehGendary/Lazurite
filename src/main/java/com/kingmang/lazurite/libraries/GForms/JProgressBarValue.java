package com.kingmang.lazurite.libraries.GForms;


import com.kingmang.lazurite.core.*;
import com.kingmang.lazurite.runtime.LZR.LZRFunction;
import com.kingmang.lazurite.runtime.LZR.LZRNumber;
import com.kingmang.lazurite.runtime.Value;

import javax.swing.JProgressBar;

public class JProgressBarValue extends JComponentValue {

    final JProgressBar progressBar;

    public JProgressBarValue(JProgressBar progressBar) {
        super(19, progressBar);
        this.progressBar = progressBar;
        init();
    }

    private void init() {
        set("onChange", new LZRFunction(this::addChangeListener));
        set("addChangeListener", new LZRFunction(this::addChangeListener));
        set("getMinimum", Converters.voidToInt(progressBar::getMinimum));
        set("getMaximum", Converters.voidToInt(progressBar::getMaximum));
        set("getOrientation", Converters.voidToInt(progressBar::getOrientation));
        set("getValue", Converters.voidToInt(progressBar::getValue));
        set("isBorderPainted", Converters.voidToBoolean(progressBar::isBorderPainted));
        set("isIndeterminate", Converters.voidToBoolean(progressBar::isIndeterminate));
        set("isStringPainted", Converters.voidToBoolean(progressBar::isStringPainted));
        set("getString", Converters.voidToString(progressBar::getString));
        set("getPercentComplete", Converters.voidToDouble(progressBar::getPercentComplete));
        
        set("setMinimum", Converters.intToVoid(progressBar::setMinimum));
        set("setMaximum", Converters.intToVoid(progressBar::setMaximum));
        set("setBorderPainted", Converters.booleanToVoid(progressBar::setBorderPainted));
        set("setIndeterminate", Converters.booleanToVoid(progressBar::setIndeterminate));
        set("setOrientation", Converters.intToVoid(progressBar::setOrientation));
        set("setStringPainted", Converters.booleanToVoid(progressBar::setStringPainted));
        set("setString", Converters.stringToVoid(progressBar::setString));
        set("setValue", Converters.intToVoid(progressBar::setValue));
    }
    
    private Value addChangeListener(Value[] args) {
        Arguments.check(1, args.length);
        final Function action = ValueUtils.consumeFunction(args[0], 0);
        progressBar.addChangeListener(e -> action.execute());
        return LZRNumber.ZERO;
    }
}