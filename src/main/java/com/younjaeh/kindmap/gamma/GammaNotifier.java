package com.younjaeh.kindmap.gamma;

public interface GammaNotifier {
    GammaNotifier NONE = percent -> {
    };

    void showGammaPercent(int percent);
}
