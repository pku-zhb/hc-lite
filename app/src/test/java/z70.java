import android.content.res.Resources;
import android.view.View;

/** Unrelated R8-shaped native candidate, with JVM-safe resources and a generated ID. */
@SuppressWarnings("deprecation")
public final class z70 extends View {
    private final Resources resources = new Resources(null, null, null);
    public z70() { super(null); }
    @Override public Resources getResources() { return resources; }
}
