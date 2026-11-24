package uz.focusuz.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;
import java.util.Locale;

public class FocusService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        String pkg = event.getPackageName().toString();
        String target;

        if (pkg.equals("com.google.android.youtube")) {
            target = "shorts";
        } else if (pkg.equals("com.instagram.android")) {
            target = "reels";
        } else {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        boolean detected = containsTarget(root, target, 0);
        root.recycle();

        if (detected) {
            Log.d("FocusUZ", target + " detected");
        }
    }

    private boolean containsTarget(AccessibilityNodeInfo node,
                                   String target, int depth) {
        if (node == null || depth > 15) return false;

        CharSequence desc = node.getContentDescription();

        if (node.isSelected() && desc != null &&
            desc.toString().toLowerCase(Locale.ROOT).contains(target)) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;

            boolean found = containsTarget(child, target, depth + 1);
            child.recycle();

            if (found) return true;
        }

        return false;
    }

    @Override
    public void onInterrupt() {}
}
