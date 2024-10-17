package GettingGoodComeback;

import aic2024.user.*;

import java.util.ArrayList;

public class HQ {
    Direction[] directions = Direction.values();

    ArrayList<Location> structures=new ArrayList<>();
    ArrayList<Location> destroyed=new ArrayList<>();
    public HQ(UnitController uc) {
        while (true) {
            // if enemies are seen, spam like crazy, especially drones
            if (uc.senseAstronauts(GameConstants.HQ_VISION_RANGE,uc.getOpponent()).length>0) {
                for (Direction dir : directions) {
                    if (uc.canEnlistAstronaut(dir, 30, null)) {
                        if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.REINFORCED_SUIT)>0) {
                            uc.enlistAstronaut(dir,30,CarePackage.REINFORCED_SUIT);
                        }
                        else {
                            uc.enlistAstronaut(dir, 30, null);
                        }
                    }
                }
            }
            //Spawn exactly one astronaut with 30 oxygen, if possible
            for (Direction dir : directions) {
                if (uc.canEnlistAstronaut(dir, 80, null)) {
                    if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.SURVIVAL_KIT)>0) {
                        uc.enlistAstronaut(dir,40,CarePackage.SURVIVAL_KIT);
                    }
                    else if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.REINFORCED_SUIT)>0) {
                        uc.enlistAstronaut(dir,80,CarePackage.REINFORCED_SUIT);
                    }
                    else {
                        uc.enlistAstronaut(dir, 30, null);
                    }
                    break;
                }
            }
            BroadcastInfo message = uc.pollBroadcast();
            while (message != null) {
                uc.println(message.getMessage());
                Location location=new Location(message.getMessage()%(int)Math.pow(2,12)%GameConstants.MAX_MAP_SIZE,(message.getMessage()%(int)Math.pow(2,12))/GameConstants.MAX_MAP_SIZE);

                if (message.getMessage()/(int)Math.pow(2,12)==1) {
                    if (structures.contains(location)) {
                        structures.remove(location);
                    }
                    if (!destroyed.contains(location)) {
                        destroyed.add(location);
                    }
                }
                else {
                    if (!structures.contains(location) && !destroyed.contains(location)) {
                        structures.add(location);
                        uc.println(location.x);
                        uc.println(location.y);
                    }
                }
                message = uc.pollBroadcast();
            }
            for (Location location:structures) {
                if (uc.canPerformAction(ActionType.BROADCAST,Direction.ZERO,location.y*GameConstants.MAX_MAP_SIZE+location.x)) {
                    uc.performAction(ActionType.BROADCAST,Direction.ZERO,location.y*GameConstants.MAX_MAP_SIZE+location.x);
                }
            }
            for (Location location:destroyed) {
                if (uc.canPerformAction(ActionType.BROADCAST,Direction.ZERO,(int)Math.pow(2,12)+location.y*GameConstants.MAX_MAP_SIZE+location.x)) {
                    uc.performAction(ActionType.BROADCAST,Direction.ZERO,(int)Math.pow(2,12)+location.y*GameConstants.MAX_MAP_SIZE+location.x);
                }
            }
            uc.yield();
        }
    }
}