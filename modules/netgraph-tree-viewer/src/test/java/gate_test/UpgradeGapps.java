/*
 * Copyright (c) 2020 Datlowe and/or its affiliates. All rights reserved.
 */

package gate_test;

import gate.util.persistence.UpgradeXGAPP;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.util.List;

public class UpgradeGapps {

    public static void main(String[] args) throws Exception {

        File originalXGapp = new File("C:\\Workspace\\common-resources\\Linguistics\\cs\\gate_apps\\cs_morphodita.gapp");

        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(originalXGapp);
        List<UpgradeXGAPP.UpgradePath> upgrades = UpgradeXGAPP.suggest(doc);

        for (UpgradeXGAPP.UpgradePath u : upgrades) {
            System.err.println("-------------------------");
            System.err.println(u.getUpgradeStrategy().label);
            System.err.println(u.getOldPath());
            System.err.println(u.getNewPath());

        }
        
        //TODO: not finished, not needed, see gate.util.persistence.UpgradeXGAPP.main()

    }
}
