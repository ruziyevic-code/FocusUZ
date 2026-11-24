package uz.focusuz.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

public class FocusService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        String pkg = event.getPackageName().toString();

        if (!pkg.equals("com.google.android.youtube")) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        boolean shorts = containsShorts(root, 0);
        root.recycle();

        if (shorts) {
            Log.d("FocusUZ", "YouTube Shorts belgisi aniqlandi");
        }
    }

    private boolean containsShorts(AccessibilityNodeInfo node, int depth) {
        if (node == null || depth > 15) return false;

        CharSequence desc = node.getContentDescription();

        if (desc != null &&
            desc.toString().toLowerCase().contains("shorts")) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null && containsShorts(child, depth + 1)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {
    }
}
