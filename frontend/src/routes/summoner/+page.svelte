<script lang="ts">
    import { page } from '$app/stores';
    import SummonerInfo from './SummonerInfo.svelte';
    import SummonerMatches from "./SummonerMatches.svelte";
    import MatchResultNull from "../errorpage/MatchResultNull.svelte";
    import {onMount} from "svelte";

    let info: any = {};
    let recent: any = {};
    let matches: any = [];
    let version: any = "";
    let smn = $page.url.searchParams.get('smn');
    let matchResultCode: number = 1000;

    onMount(
        async() => {
            try {
                console.log("fetchData : ", smn);
                // 병렬 호출
                await fetchMatchWithName();
                await fetchInfo();
            } catch (error) {
                console.error('Error fetching data:', error);
            }
        });

    // 소환사 정보
    async function fetchInfo() {
        try {
            const url = `http://localhost:8088/api/summoner?smn=${encodeURIComponent(smn)}`
            console.log(url)
            const response = await fetch(url, { mode: 'cors' });
            if (!response.ok) {
                throw new Error('Failed to fetch summoner info');
            }
            const data = await response.json();
            if (data.code != null && data.code != 0) {
                console.log('data code : ', data.code)
                throw new Error('summoner info is not successful');
            }
            console.log('info data code : ', data.code)
            info = data.data;
        } catch (error) {
            console.error('Error fetching summoner info:', error);
            // 오류 발생 시 리다이렉트
        }
    }

    // Match 정보
    async function fetchMatchWithName() {
        try {
            const url = `http://localhost:8088/api/matches?smn=${encodeURIComponent(smn)}`
            console.log(url)
            const response = await fetch(url, { mode: 'cors' });
            if (!response.ok) {
                throw new Error('Failed to fetch matches');
            }
            const data = await response.json();
            matchResultCode = data.code;
            if (matchResultCode != null && matchResultCode != 0) {
                console.log('matches data code : ', matchResultCode)
                throw new Error('summoner matches is not successful');
            }
            //console.log("matches", data.data)
            recent = data.data.recent;
            matches = data.data.matchList;
            version = data.data.version;
        } catch (error) {
            console.error('Error fetching matches:', error);
            // 오류 발생 시 이미지 띄우기
        }
    }

</script>

<main>
    <div class="container main-inner">
        <div class="main-card mb-3 card" style="height:360px">
            {#if info.name && recent }
                <div class="card-img-overlay" style="height: 360px; background: linear-gradient(to right, rgba(0,0,0,0.9) 40%, rgba(0,0,0,0) 100%), url('http://ddragon.leagueoflegends.com/cdn/img/champion/splash/{info.masteryChamp}_0.jpg'); background-size: cover; background-position: right;">
                    <SummonerInfo {info}{recent}/>
                </div>
            {:else}
                <div style="display: flex; justify-content: center; align-items: center; height: 100vh;">
                    <img src="/src/lib/images/img/loading.gif" width="10%">
                </div>
            {/if}
        </div>

        {#if matchResultCode != 1000}
            {#if matchResultCode == 0}
                <SummonerMatches {matches}{version}/>
            {:else }
                <MatchResultNull />
            {/if}
        {:else}
            <p>Loading...</p>
        {/if}
    </div>

</main>
