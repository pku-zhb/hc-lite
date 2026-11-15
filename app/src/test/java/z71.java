import android.view.View;
import com.zhuhuibin.hypermusiccover.duo.NativeMaterialTarget;

/** R8-shaped target: no module namespace survives repackaging. */
public final class z71 extends View implements NativeMaterialTarget {
    public int version = 1;
    public int claims;
    public int reads;
    public z71() { super(null); }
    @Override public int hmcMaterialContractVersion() { reads++; return version; }
    @Override public int hmcMaterialState() { reads++; return 0; }
    @Override public View hmcMaterialOwner() { reads++; return null; }
    @Override public boolean hmcMaterialClaim(int version) { claims++; return false; }
    @Override public boolean hmcMaterialReapply() { throw new AssertionError("Unregistered probe cannot reapply"); }
}
