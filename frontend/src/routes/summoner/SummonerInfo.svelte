<script lang="ts">
    import { onMount } from "svelte";
    import Chart from 'chart.js/auto';

    export let info: any;
    export let recent: any;
    let chartCanvas;
    let win: number;
    let lose: number;

    // onMount 함수에서 recent 데이터를 가져온 후에 initChart 호출
    onMount(async () => {
        if (recent) {
            lose = recent.lose;
            win = recent.win;
            await initChart(); // 차트 초기화 함수 호출
        }
    });

    async function initChart() {
        if (!recent) {
            console.error("info and/or recent objects are missing or not loaded.");
            return;
        }

        let ctx = chartCanvas.getContext("2d");

        var chart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    label: '# of Votes',
                    data: [lose, win],
                    backgroundColor: [
                        'rgba(255,99,132,0.2)',
                        'rgba(54,162,235,0.2)',
                    ],
                    borderColor: [
                        'rgba(255,99,132,1)',
                        'rgba(54,162,235,1)',
                    ],
                    borderWidth: 1
                }]
            },
            options: {},
        });
    }

</script>


    <div class="row summoner-date2">

        <!-- 아이콘 -->
        <div class="col-sm-3 m-4 summoner">
            <img src='https://ddragon.leagueoflegends.com/cdn/{info.version}/img/profileicon/{info.profileIconId}.png' alt="소환사 아이콘" width="120px">
        </div>

        <!-- 소환사 명 -->
        <div class="col-sm-7 mt-4 summoner">
            <h1 class="mt-3">{info.name}</h1>
            <h5><span class="badge badge-secondary">Lv.{info.summonerLevel}</span> </h5>
        </div>
    </div>
    <!-- 검색한 소환사 info 하단 -->
    <div class="row summoner-data4 m-3">
        <!-- box 1 -->
        <div class="col-sm-3">
            <div class="row text-right">
                <!-- 솔로랭크 -->
                <!-- 언랭일때 -->
                {#if info.soloTier == 'Unranked'}
                    <img src="/src/lib/images/img/rank1.png" alt="Unranked" width="170px">
                {:else}
                    <div class="col-sm-5">
                        <img src={`/src/lib/images/emblem/${info.soloTier}.png`} alt="tier" width="80px">
                    </div>
                    <div class="col-sm-6">
                        <h6 class="small">솔로랭크</h6>
                        <h6>{info.soloTier} {info.soloRank}</h6>
                        <h6 class="small">{info.soloLeaguePoints} P</h6>
                        <h6 class="small">{info.soloWins}승 {info.soloLosses}패 </h6>
                    </div>
                {/if}
            </div>
        </div>
        <!-- box 2 -->
        <div class="col-sm-3">
            <div class="row text-right">
                <!-- 자유랭크 -->
                <!-- 언랭일때 -->
                {#if info.flexTier == 'Unranked'}
                    <img src="/src/lib/images/img/rank2.png" alt="Unranked" width="170px">
                {:else}
                    <div class="col-sm-5">
                        <img src={`/src/lib/images/emblem/${info.flexTier}.png`} alt="tier" width="80px">
                    </div>
                    <div class="col-sm-6">
                        <h6 class="small">자유랭크</h6>
                        <h6>{info.flexTier} {info.flexRank}</h6>
                        <h6 class="small">{info.flexLeaguePoints} P</h6>
                        <h6 class="small">{info.flexWins}승 {info.flexLosses}패 </h6>
                    </div>
                {/if}
            </div>
        </div>
        <!-- box 3 최근 20전 승률 -->
        <div class="col-sm-3">
            <div class="row">
                <!-- 도넛 차트 -->
                    <div class="col-sm-5">
                        <canvas bind:this={chartCanvas} id="myChart" width="230" height="330"/>
                    </div>
                    <div class="col-sm-6 text-right">
                        <h6>{recent.win + recent.lose}전 {recent.win}승 {recent.lose}패</h6>
                        <h6>{recent.avg}</h6>
                        <h6 class="small">{recent.kill} / {recent.death} / {recent.assist}</h6>
                    </div>

            </div>
        </div>
        <!-- box 4 인게임 여부 -->
        <div class="col-sm-3 text-center">
            {#if !info.current}
                <img src="/src/lib/images/img/current.png" alt="게임중 아님" width="150px">
            {:else}
                <p>{info.curChamp} playing!</p>
                <img src='https://ddragon.leagueoflegends.com/cdn/{info.version}/img/champion/{info.curChamp}.png' alt="현재 챔프" width="70px">
            {/if}
        </div>
    </div>
    <!-- info 하단 종료 -->
