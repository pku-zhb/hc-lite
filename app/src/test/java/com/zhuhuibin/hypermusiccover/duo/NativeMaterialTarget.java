package com.zhuhuibin.hypermusiccover.duo;

import android.view.View;

/** A test-only copy of the optional public ABI; production has no HMC dependency. */
public interface NativeMaterialTarget {
    int hmcMaterialContractVersion();
    int hmcMaterialState();
    View hmcMaterialOwner();
    boolean hmcMaterialClaim(int version);
    boolean hmcMaterialReapply();
}
