package me.hexu.resolver.interfaces.callbacks;

import java.util.ArrayList;
import java.util.HashMap;

public interface ISearchBaseResponseCallback {
    void onResponseReceived(HashMap<String, ArrayList<String>> searchBase);
}
