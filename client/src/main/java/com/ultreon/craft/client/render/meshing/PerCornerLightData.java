package com.ultreon.craft.client.render.meshing;

public class PerCornerLightData {
    public float l00, l01, l10, l11;

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(o == this)
            return true;
        if(o instanceof PerCornerLightData p) {
            return p.l10 == this.l10 && p.l11 == this.l11 && p.l00 == this.l00 && p.l01 == this.l01;
        }
        return false;
    }
}
