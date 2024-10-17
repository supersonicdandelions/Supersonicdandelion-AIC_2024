package GettingGood;

import aic2024.user.*;

import java.util.ArrayList;


public class Astronaught {
    Direction[] directions = Direction.values();
    public Astronaught(UnitController uc) {
        int dirIndex = (int) (uc.getRandomDouble() * 8.0);
        Direction randomDir = directions[dirIndex];
        while (true) {
            //move randomly, turning right if we can't move.

            if (uc.canPerformAction(ActionType.MOVE,Direction.ZERO,0)) {
                if (uc.canPerformAction(ActionType.MOVE,randomDir,0)) {
                    uc.performAction(ActionType.MOVE,randomDir,0);
                }
                else {
                    ArrayList<Direction> moveable=new ArrayList<>();
                    for (Direction dir: directions) {
                        if (!dir.isEqual(Direction.ZERO)) {
                            if (uc.canPerformAction(ActionType.MOVE, dir, 0)) {
                                moveable.add(dir);
                            }
                        }
                    }
                    if (!moveable.isEmpty()) {
                        randomDir = moveable.get((int) (uc.getRandomDouble() * moveable.size()));
                        uc.performAction(ActionType.MOVE,randomDir,0);
                    }
                }
            }

            if (uc.senseStructures(2,uc.getOpponent()).length>0) {
                for (StructureInfo structure:uc.senseStructures(2,uc.getOpponent())) {
                    if (uc.canPerformAction(ActionType.SABOTAGE,uc.getLocation().directionTo(structure.getLocation()),0)) {
                        uc.performAction(ActionType.SABOTAGE,uc.getLocation().directionTo(structure.getLocation()),0);
                    }
                }
            }

            float maxOxygen=maxOxygen(uc.senseAstronauts(2,uc.getOpponent()));
            if (uc.senseAstronauts(2,uc.getOpponent()).length>0 & maxOxygen>uc.getAstronautInfo().getOxygen()) {
                for (AstronautInfo astronautOpponent: uc.senseAstronauts(2,uc.getOpponent())) {
                    if (uc.canPerformAction(ActionType.SABOTAGE,uc.getLocation().directionTo(astronautOpponent.getLocation()),0) & astronautOpponent.getOxygen()==maxOxygen) {
                        uc.performAction(ActionType.SABOTAGE,uc.getLocation().directionTo(astronautOpponent.getLocation()),0);
                    }
                }
            }

            //Check if there are Care Packages at an adjacent tile. If so, retrieve them.
            for (Direction dir : directions) {
                Location adjLocation = uc.getLocation().add(dir);
                if (!uc.canSenseLocation(adjLocation)) continue;
                CarePackage cp = uc.senseCarePackage(adjLocation);
                if (cp != null & (cp==CarePackage.PLANTS | cp==CarePackage.OXYGEN_TANK | cp==CarePackage.SURVIVAL_KIT)) {
                    if (uc.canPerformAction(ActionType.RETRIEVE, dir, 0)) {
                        uc.performAction(ActionType.RETRIEVE, dir, 0);
                        break;
                    }
                }
            }

            //If we have 1 or 2 oxygen left, terraform my tile (alternatively, terraform a random tile)
            if (uc.getAstronautInfo().getOxygen() <= 2) {
                if (uc.canPerformAction(ActionType.TERRAFORM, Direction.ZERO, 0)) {
                    uc.performAction(ActionType.TERRAFORM, Direction.ZERO, 0);
                }
                else {
                    dirIndex = (int) (uc.getRandomDouble() * 8.0);
                    randomDir = directions[dirIndex];
                    for (int i = 0; i < 8; ++i) {
                        //Note that the 'value' of the following command is irrelevant.
                        if (uc.canPerformAction(ActionType.TERRAFORM, randomDir, 0)) {
                            uc.performAction(ActionType.TERRAFORM, randomDir, 0);
                            break;
                        }
                        randomDir = randomDir.rotateRight();
                    }
                }
            }
            uc.yield();
        }
    }
    public float maxOxygen(AstronautInfo[] astronautInfos) {
        float max=0;
        for (AstronautInfo astronaut: astronautInfos) {
            max = Math.max(max,astronaut.getOxygen());
        }
        return max;
    }
}
