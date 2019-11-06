package com.thinkernote.ThinkerNote.views.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.thinkernote.ThinkerNote.R;

public class MenuDialog extends Dialog {  
	
    public MenuDialog(Context context) {  
        super(context);  
    }  
  
    public MenuDialog(Context context, int theme) {  
        super(context, theme);  
    }  
    
    public static class Builder {  
        private Context context;  
        private View contentView;  
        private MenuDialog dialog;
  
        public Builder(Context context) {  
            this.context = context;  
        }  
  
        public Builder setContentView(View v) {  
            this.contentView = v;  
            return this;  
        }  
        
        public void destroy() {
        	dialog.dismiss();
        }
  
        public MenuDialog create() {  
            LayoutInflater inflater = (LayoutInflater) context  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
            dialog = new MenuDialog(context, R.style.Dialog);  
            View layout = inflater.inflate(R.layout.dialog_menu_layout, null);  
            dialog.addContentView(layout, new LayoutParams(  
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
            
            if (contentView != null) {  
                // if no message set  
                // add the contentView to the dialog body  
                ((FrameLayout) layout.findViewById(R.id.menu_dialog_layout))  
                        .removeAllViews();  
                ((FrameLayout) layout.findViewById(R.id.menu_dialog_layout))  
                        .addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));  
            }  
            
            dialog.setContentView(layout);  
            return dialog;  
        }  
    }  
}  
