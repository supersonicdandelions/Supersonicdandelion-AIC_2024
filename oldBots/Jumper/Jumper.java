package Jumper;

import aic2024.user.*;

import java.util.ArrayList;

public class Jumper {

    Direction[] directions = Direction.values();
    public Jumper (UnitController uc) {
        if (uc.senseObjectAtLocation(uc.getLocation())!=MapObject.HYPERJUMP) {
            while (true) {
                if (uc.canPerformAction(ActionType.BUILD_HYPERJUMP,Direction.ZERO,0)) {
                    uc.performAction(ActionType.BUILD_HYPERJUMP,Direction.ZERO,0);
                }
                else {
                    uc.yield();
                }
            }
        }
        int dirIndex = (int) (uc.getRandomDouble() * 8.0);
        Direction randomDir = directions[dirIndex];
        while (true) {
            if (!canJump(uc,randomDir)) {
                if (uc.canPerformAction(ActionType.MOVE, randomDir, 0)) {
                    uc.performAction(ActionType.MOVE, randomDir, 0);
                } else if (checkIfBuild(uc, randomDir)) {
                    if (uc.canPerformAction(ActionType.BUILD_HYPERJUMP, Direction.ZERO, 0)) {
                        uc.performAction(ActionType.BUILD_HYPERJUMP, Direction.ZERO, 0);
                    }
                } else {
                    ArrayList<Direction> moveable = new ArrayList<>();
                    for (Direction dir : directions) {
                        if (!dir.isEqual(Direction.ZERO)) {
                            if (uc.canPerformAction(ActionType.MOVE, dir, 0)) {
                                moveable.add(dir);
                            }
                        }
                    }
                    if (!moveable.isEmpty()) {
                        randomDir = moveable.get((int) (uc.getRandomDouble() * moveable.size()));
                        uc.performAction(ActionType.MOVE, randomDir, 0);
                    }
                }
            }
            if (uc.getAstronautInfo().getOxygen() <= 1) {
                if (uc.canPerformAction(ActionType.BUILD_HYPERJUMP, Direction.ZERO, 0)) {
                    uc.performAction(ActionType.BUILD_HYPERJUMP, Direction.ZERO, 0);
                }
                else {
                    dirIndex = (int) (uc.getRandomDouble() * 8.0);
                    randomDir = directions[dirIndex];
                    for (int i = 0; i < 8; ++i) {
                        //Note that the 'value' of the following command is irrelevant.
                        if (uc.canPerformAction(ActionType.BUILD_HYPERJUMP, randomDir, 0)) {
                            uc.performAction(ActionType.BUILD_HYPERJUMP, randomDir, 0);
                            break;
                        }
                        randomDir = randomDir.rotateRight();
                    }
                }
            }
            uc.yield();
        }
    }
    public boolean checkIfBuild(UnitController uc, Direction dir) {
        Location empty = uc.getLocation().add(dir);
        if (uc.senseTileType(empty) == TileType.WATER) {
            for (int i = 1; i < GameConstants.MAX_JUMP; i++) {
                empty = empty.add(dir);
                if (uc.senseTileType(empty) != TileType.WATER) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean canJump(UnitController uc, Direction dir) {
        for (int i=GameConstants.MAX_JUMP;i>0;i--) {
            if (uc.canPerformAction(ActionType.JUMP,dir,i)) {
                uc.performAction(ActionType.JUMP,dir,i);
                return true;
            }
        }
        return false;
    }
}
