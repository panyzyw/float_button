package com.yongyida.robot.floatbutton.classes;

/**
 * @author Bright. Create on 2017/3/9.
 */
public interface IFloatButton {

    void onCreate();
    void setVisible(int mode);

    void performSingleClick();
    void performDoubleClick();
    void performLongClick();
    void setButtonHide(boolean isHide);

}
