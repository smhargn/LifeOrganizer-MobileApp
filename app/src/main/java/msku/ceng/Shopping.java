package msku.ceng;

import android.widget.CheckBox;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Shopping {

    private String shoppingText;
    public boolean isChecked;

    public Shopping(String shoppingText, boolean isChecked) {
        this.shoppingText = shoppingText;
        this.isChecked = isChecked;
    }

    public String getShoppingText() {
        return shoppingText;
    }

    public void setShoppingText(String shoppingText) {
        this.shoppingText = shoppingText;
    }

    public boolean getShoppingCheck() {
        return isChecked;
    }

    public void setShoppingCheck(boolean checked) {
        this.isChecked = checked;
    }






}