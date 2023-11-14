package cs2.dma.main;

import cs2.dma.entry.PlayerInfo;
import cs2.dma.tuil.MemoryTool;

public class PlayerAddressUpdateThread extends Thread {
    private int index;
    private PlayerInfo playerInfo;
    private boolean isKnowMap;
    private float levelHeight = 250;
    private MemoryTool memoryTool;
    private long EntityList;
    private long clientAddress;

    private long LocalPlayerController;

    private long dwEntityList;

    // Offsets
    private static final int m_iPawnHealth = 0x7E0;     //client.dll m_iPawnHealth
    private static final int m_iPawnArmor = 0x7E4;      //client.dll m_iPawnArmor
    private static final int m_bPawnIsAlive = 0x7DC;    //client.dll m_bPawnIsAlive
    private static final int m_angEyeAngles = 0x1510;   //client.dll m_angEyeAngles
    private static final int m_iTeamNum = 0x3BF;        //client.dll m_iTeamNum
    private static final int m_hPlayerPawn = 0x7D4;     //client.dll m_hPlayerPawn
    private static final int Player_Position = 0xCD8;   //PlayerPosition X //+ 0x4 Y //+ 0x8 Z

    public void setKnowMap(boolean knowMap) {
        isKnowMap = knowMap;
    }

    public void setLocalPlayerController(long localPlayerController) {
        LocalPlayerController = localPlayerController;
    }

    public void setEntityList(long entityList) {
        EntityList = entityList;
    }

    public void setClientAddress(long clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setDwEntityList(long dwEntityList) {
        this.dwEntityList = dwEntityList;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMemoryTool(MemoryTool memoryTool) {
        this.memoryTool = memoryTool;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public void run() {
        long EntityAddress = memoryTool.readAddress(EntityList + (index + 1) * 0x78, 8);
        if (EntityAddress == 0)
            return;
        long EntityPawnListEntry = memoryTool.readAddress(clientAddress + dwEntityList, 8);
        if (EntityPawnListEntry == 0)
            return;
        long Pawn = memoryTool.readAddress(EntityAddress + m_hPlayerPawn, 8);
        if (Pawn == 0)
            return;
        EntityPawnListEntry = memoryTool.readAddress(EntityPawnListEntry + 0x10 + 8 * ((Pawn & 0x7FFF) >> 9), 8);
        Pawn = memoryTool.readAddress(EntityPawnListEntry + 0x78 * (Pawn & 0x1FF), 8);
        if (Pawn == 0)
            return;
        float localPlayerZ = memoryTool.readFloat(LocalPlayerController + Player_Position + 0x8, 4);
        if (isKnowMap) {
            int teamId = memoryTool.readInt(EntityAddress + m_iTeamNum, 4);
            float playerZ = memoryTool.readFloat(Pawn + Player_Position + 0x8, 4);
            float levelDv = playerZ - localPlayerZ;
            levelDv = (levelDv < 0) ? -levelDv : levelDv;
            playerInfo = new PlayerInfo(
                    EntityAddress,
                    Pawn,
                    teamId,
                    memoryTool.readInt(EntityAddress + m_iPawnHealth, 4),
                    memoryTool.readInt(EntityAddress + m_iPawnArmor, 4),
                    memoryTool.readInt(EntityAddress + m_bPawnIsAlive, 4) != 0,
                    LocalPlayerController == Pawn,
                    memoryTool.readInt(LocalPlayerController + m_iTeamNum, 4) != teamId,
                    memoryTool.readFloat(Pawn + Player_Position + 0x4, 4),
                    memoryTool.readFloat(Pawn + Player_Position, 4),
                    playerZ,
                    90 - memoryTool.readFloat(Pawn + m_angEyeAngles + 0x4, 8),
                    levelDv < levelHeight);
        } else {
            float angle = memoryTool.readFloat(LocalPlayerController + m_angEyeAngles + 0x4, 8) - 90;
            int teamId = memoryTool.readInt(EntityAddress + m_iTeamNum, 4);
            float pX = memoryTool.readFloat(Pawn + Player_Position + 0x4, 4);
            float pY = memoryTool.readFloat(Pawn + Player_Position, 4);
            float newX = pX * (float) Math.cos(Math.toRadians(angle)) - pY * (float) Math.sin(Math.toRadians(angle));
            float newY = pX * (float) Math.sin(Math.toRadians(angle)) + pY * (float) Math.cos(Math.toRadians(angle));

            float playerZ = memoryTool.readFloat(Pawn + Player_Position + 0x8, 4);
            float levelDv = playerZ - localPlayerZ;
            levelDv = (levelDv < 0) ? -levelDv : levelDv;

            playerInfo = new PlayerInfo(
                    EntityAddress,
                    Pawn,
                    teamId,
                    memoryTool.readInt(EntityAddress + m_iPawnHealth, 4),
                    memoryTool.readInt(EntityAddress + m_iPawnArmor, 4),
                    memoryTool.readInt(EntityAddress + m_bPawnIsAlive, 4) != 0,
                    LocalPlayerController == Pawn,
                    memoryTool.readInt(LocalPlayerController + m_iTeamNum, 4) != teamId,
                    newX,
                    newY,
                    playerZ,
                    90 - memoryTool.readFloat(Pawn + m_angEyeAngles + 0x4, 8) + angle,
                    levelDv < levelHeight);
        }
    }

}
