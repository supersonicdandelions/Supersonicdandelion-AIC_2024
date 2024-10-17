package MyPlayer;

import aic2024.user.*;

public class HQ {
    Direction[] directions = Direction.values();
    public HQ(UnitController uc) {
        while (true) {
            //Spawn exactly one astronaut with 30 oxygen, if possible
            for (Direction dir : directions) {
                if (uc.canEnlistAstronaut(dir, 20, null)) {
                    if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.SURVIVAL_KIT)>0) {
                        uc.enlistAstronaut(dir,20,CarePackage.SURVIVAL_KIT);
                    }
                    uc.enlistAstronaut(dir, 20, null);
                    break;
                }
            }
            uc.yield();
        }
    }
}