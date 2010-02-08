package com.tinkerpop.restling.domain;

import java.util.ArrayList;

/**
 * Needed to handle list URI template expansions which aren't supported in Jersy at the moment.
 */
@SuppressWarnings("serial")
public class AmpersandSeparatedList extends ArrayList<String> {
    public AmpersandSeparatedList(String s) {
        for (String e : s.split("&") ) {
            if (e.trim().length() > 0)
                add(e);
        }
    }

    public AmpersandSeparatedList() { }
}