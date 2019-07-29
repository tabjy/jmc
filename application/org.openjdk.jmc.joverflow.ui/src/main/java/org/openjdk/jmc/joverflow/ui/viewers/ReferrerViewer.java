package org.openjdk.jmc.joverflow.ui.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.openjdk.jmc.joverflow.support.RefChainElement;
import org.openjdk.jmc.joverflow.ui.model.ModelListener;
import org.openjdk.jmc.joverflow.ui.model.ObjectCluster;

public class ReferrerViewer implements ModelListener {

    private final ReferrerTable ui;
    private ReferrerItemBuilder builder;

//    private final Callback<RefChainElement, Boolean> filter = new Callback<RefChainElement, Boolean>() {
//
//        @Override
//        public Boolean call(RefChainElement param) {
//            return ui.selectedItem == null || ui.selectedItem.check(param);
//            return null;
//        }
//    };

    public ReferrerViewer(Runnable updateCallback) {
//        ui = new ReferrerTable(updateCallback);
        ui = null;
    }

    @Override
    public void allIncluded() {
        if (builder == null) {
//            ui.getItems().clear();
        } else {
//            ui.set(builder.buildReferrerList());
            builder = null;
        }
    }

    @Override
    public void include(ObjectCluster oc, RefChainElement ref) {
        if (builder == null) {
            builder = new ReferrerItemBuilder(oc, ref);
        } else {
            builder.addCluster(oc, ref);
        }
    }

    public TableViewer getUi() {
//        return ui;
        return null;
    }

//    public Callback<RefChainElement, Boolean> getFilter() {
//        return filter;
//    }

    public void reset() {
//        ui.selectedItem = null;
    }
}