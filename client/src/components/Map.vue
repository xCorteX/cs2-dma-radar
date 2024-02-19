<template>
    <div class="control">
        <div>Number of players:{{ playerNum }}</div>
        <div>Tick:{{ gameInfo.tick }}</div>
        <div>Average tick:{{ parseInt(allTickVal / tickTimes) }}</div>
        <div @click="opchange()">Follow character<input type="checkbox" v-model="isOpenFlow" /></div>
        <div @click="showTeammatesChange()">Show teammates<input type="checkbox" v-model="showTeammates" /></div>
        <div @click="showEnemiesChanges()">Show enemies<input type="checkbox" v-model="showEnemies" /></div>
    </div>
    <div id="map"></div>
</template>

<script>
import localPlayerIcon from '/src/icons/localPlayer_icon.png'
import enemyIcon from '/src/icons/enemy_icon.png'
import teammateIcon from '/src/icons/teammate_icon.png'
import enemyIconHvd from '/src/icons/enemy_icon_hvd.png'
import teammateIconHvd from '/src/icons/teammate_icon_hvd.png'

import cs_office_radar from '/src/map/cs_office_radar.png'
import de_ancient_radar from '/src/map/de_ancient_radar.png'
import de_anubis_radar from '/src/map/de_anubis_radar.png'
import de_dust2_radar from '/src/map/de_dust2_radar.png'
import de_inferno_radar from '/src/map/de_inferno_radar.png'
import de_mirage_radar from '/src/map/de_mirage_radar.png'
import de_nuke_lower_radar from '/src/map/de_nuke_lower_radar.png'
import de_nuke_radar from '/src/map/de_nuke_radar.png'
import de_overpass_radar from '/src/map/de_overpass_radar.png'
import de_vertigo_lower_radar from '/src/map/de_vertigo_lower_radar.png'
import de_vertigo_radar from '/src/map/de_vertigo_radar.png'

var that
var mapRadar = {
    cs_office: {
        map: cs_office_radar,
        bounds: [
            [-234, -184],
            [185, 240]
        ]
    },
    de_ancient: {
        map: de_ancient_radar,
        bounds: [
            [-294, -289],
            [217, 213]
        ]
    },
    de_anubis: {
        map: de_anubis_radar,
        bounds: [
            [-200, -275],
            [333, 250]
        ]
    },
    de_dust2: {
        map: de_dust2_radar,
        bounds: [
            [-127, -247],
            [323, 202]
        ]
    },
    de_inferno: {
        map: de_inferno_radar,
        bounds: [
            [-112, -206],
            [380, 292]
        ]
    },
    de_mirage: {
        map: de_mirage_radar,
        bounds: [
            [-340, -322],
            [172, 188]
        ]
    },
    de_nuke: {
        needChangeMap: true,
        map: de_nuke_radar,
        mapLower: de_nuke_lower_radar,
        lowerValue: -480,
        bounds: [
            [-441, -329],
            [304, 357]
        ]
    },
    de_overpass: {
        map: de_overpass_radar,
        bounds: [
            [-361, -479],
            [181, 42]
        ]
    },
    de_vertigo: {
        needChangeMap: true,
        map: de_vertigo_radar,
        mapLower: de_vertigo_lower_radar,
        lowerValue: 11720,
        bounds: [
            [-223, -312],
            [172, 84]
        ]
    }
}
export default {
    data() {
        return {
            playerNum: 0,
            allTickVal: 0,
            tickTimes: 0,
            isOpenFlow: false,
            showTeammates: true,
            showEnemies: true,
            zoom: 1,
            lastMapName: null,
            gameInfo: {},
            MarkerList: [],
            map: null,
            imageOverLay: null,
            bounds: [
                [-330, -315],
                [155, 185]
            ],
            XSize: 500,
            YSize: 500
        }
    },
    created() {
        const stompClient = new StompJs.Client({
            brokerURL: 'ws://localhost:8080/radar'
        })

        stompClient.onConnect = (frame) => {
            console.log('Connected: ' + frame)
            stompClient.subscribe('/topic/radar', (radar) => {
                let msg = JSON.parse(radar.body)
                that.gameInfo.mapName = msg.mapName
                that.gameInfo.tick = msg.tick
                that.playerNum = msg.length
                that.tickTimes++
                that.allTickVal += msg.tick
                that.initPlayerList(msg.playerList)
            })
        }

        stompClient.onWebSocketError = (error) => {
            console.error('Error with websocket', error)
        }

        stompClient.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message'])
            console.error('Additional details: ' + frame.body)
        }

        stompClient.activate()
    },
    mounted() {
        that = this
        window.addEventListener('keydown', this.KeyDown, true)
        this.initMap()
    },
    methods: {
        opchange() {
            this.isOpenFlow = !this.isOpenFlow
        },
        showTeammatesChange() {
            this.showTeammates = !this.showTeammates
        },
        showEnemiesChanges() {
            this.showEnemies = !this.showEnemies
        },
        initPlayerList(data) {
            let MarkerList = []
            let knowMap = true
            if (typeof mapRadar[that.gameInfo.mapName] == 'undefined') {
                knowMap = false
            }
            if (knowMap) {
                that.initKnowMap()
            } else {
                that.initUnKnowMap()
            }
            if (data.length > 0) {
                this.updatePlayerMarker(data, knowMap)
            }
        },
        initKnowMap() {
            if (that.lastMapName != that.gameInfo.mapName) {
                that.lastMapName = that.gameInfo.mapName
                if (that.imageOverLay != null) {
                    this.allTickVal = 0
                    this.tickTimes = 0
                    that.map.removeLayer(that.imageOverLay)
                }
                this.imageOverLay = L.imageOverlay(mapRadar[that.gameInfo.mapName].map, mapRadar[that.gameInfo.mapName].bounds).addTo(this.map)
            }
        },
        initUnKnowMap() {
            if (that.imageOverLay != null) {
                this.allTickVal = 0
                this.tickTimes = 0
                that.map.removeLayer(that.imageOverLay)
                that.imageOverLay = null
            }
        },
        updatePlayerMarker(data, knowMap) {
            let mlist = []
            data.forEach((item) => {
                if (item.alive) {
                    if (!this.showEnemies && item.enemy) {
                        return
                    }

                    if (!this.showTeammates && !item.enemy) {
                        return
                    }

                    let potin = L.latLng(item.x / 10, item.y / 10)
                    let icon = L.icon({
                        iconUrl: item.localPlayer
                            ? localPlayerIcon
                            : item.enemy
                            ? item.sameLevel
                                ? enemyIcon
                                : enemyIconHvd
                            : item.sameLevel
                            ? teammateIcon
                            : teammateIconHvd,
                        iconSize: [40, 40],
                        iconAnchor: [19, 25]
                    })

                    if (item.localPlayer && this.isOpenFlow) {
                        this.map.flyTo(potin, this.map.getZoom())
                    }

                    mlist.push(this.addMarker(potin, icon, item.localPlayer ? (knowMap ? item.angles : 0) : item.angles))
                }
                if (item.localPlayer && knowMap) {
                    if (mapRadar[that.gameInfo.mapName].needChangeMap) {
                        if (item.z > mapRadar[that.gameInfo.mapName].lowerValue) {
                            that.map.removeLayer(that.imageOverLay)
                            this.imageOverLay = L.imageOverlay(mapRadar[that.gameInfo.mapName].map, mapRadar[that.gameInfo.mapName].bounds).addTo(
                                this.map
                            )
                        } else {
                            that.map.removeLayer(that.imageOverLay)
                            this.imageOverLay = L.imageOverlay(
                                mapRadar[that.gameInfo.mapName].mapLower,
                                mapRadar[that.gameInfo.mapName].bounds
                            ).addTo(this.map)
                        }
                    }
                }
            })
            if (that.layerGroup != null) {
                that.map.removeLayer(that.layerGroup)
            }
            that.MarkerList = mlist
            that.layerGroup = L.layerGroup(that.MarkerList)
            that.map.addLayer(that.layerGroup)
        },
        reloadMap() {
            if (that.imageOverLay != null) {
                that.map.removeLayer(that.imageOverLay)
            }
            this.bounds[1] = [this.bounds[0][0] + this.XSize, this.bounds[0][1] + this.YSize]
            this.imageOverLay = L.imageOverlay(mapRadar[that.gameInfo.mapName].map, this.bounds).addTo(this.map)
        },
        KeyDown(e) {
            if (e.keyCode == 96) {
                this.XSize = 500
                this.YSize = 500
                this.reloadMap()
            }
            //+ X
            if (e.keyCode == 98) {
                this.XSize += 1
                this.reloadMap()
            }
            //-
            if (e.keyCode == 97) {
                this.XSize -= 1
                this.reloadMap()
            }
            //+ Y
            if (e.keyCode == 101) {
                this.YSize += 1
                this.reloadMap()
            }
            //-
            if (e.keyCode == 100) {
                this.YSize -= 1
                this.reloadMap()
            }
            //←
            if (e.keyCode == 37) {
                this.bounds[0][1] += 1
                this.reloadMap()
            }
            //↑
            if (e.keyCode == 38) {
                this.bounds[0][0] -= 1
                this.reloadMap()
            }
            //→
            if (e.keyCode == 39) {
                this.bounds[0][1] -= 1
                this.reloadMap()
            }
            //↓
            if (e.keyCode == 40) {
                this.bounds[0][0] += 1
                this.reloadMap()
            }
        },
        initMap() {
            this.map = L.map('map', {
                center: [0, 0],
                zoom: this.zoom,
                crs: L.CRS.Simple,
                maxZoom: 3,
                minZoom: 0
            })
        },
        addMarker(point, icon, angles) {
            return L.marker(point, {
                icon: icon,
                rotationAngle: angles
            })
        }
    }
}
</script>

<style scoped>
#map {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 0;
}

.control {
    position: absolute;
    top: 100px;
    left: 10px;
    z-index: 1;
    background-color: aliceblue;
    border-radius: 10px;
    padding: 5px 10px;
}
</style>
