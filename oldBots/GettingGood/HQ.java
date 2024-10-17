package GettingGood;

import aic2024.user.CarePackage;
import aic2024.user.Direction;
import aic2024.user.UnitController;

public class HQ {
    Direction[] directions = Direction.values();
    public HQ(UnitController uc) {
        while (true) {
            //Spawn exactly one astronaut with 30 oxygen, if possible
            for (Direction dir : directions) {
                if (uc.canEnlistAstronaut(dir, 30, null)) {
                    if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.SURVIVAL_KIT)>0) {
                        uc.enlistAstronaut(dir,30,CarePackage.SURVIVAL_KIT);
                    }
                    uc.enlistAstronaut(dir, 30, null);
                    break;
                }
            }
            uc.yield();
        }
    }
}