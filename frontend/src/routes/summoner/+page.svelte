<script lang="ts">
    import { page } from '$app/stores';
    import SummonerInfo from './SummonerInfo.svelte';
    import SummonerMatches from "./SummonerMatches.svelte";

    let info: any = {};
    let matches: any = [];

    let smn = $page.url.searchParams.get('smn');
    let puuid = '';
    let userPuuid;
    let encodedSmn = encodeURIComponent(smn);

    fetchData();
    async function fetchData() {
            console.log("not puuid 탄다요!")
            await fetchInfo();
            await fetchMatchWithName(encodedSmn);
    }
    async function pu(puuid) {
        console.log("puuid 탄다요!")
        await fetchInfo();
        await fetchMatchWithPuuid(puuid);
    }

    // 소환사 정보
    async function fetchInfo() {
        try {
            const response1 = await fetch(`http://localhost:8088/api/summoner?smn=${encodedSmn}`, { mode: 'cors' });
            const data1 = await response1.json();
            info = data1.data;
            userPuuid = info.puuid;
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }

    // Match 정보
    async function fetchMatchWithName(encodedSmn) {
        try {
            const response2 = await fetch(`http://localhost:8088/api/matches?smn=${encodedSmn}`, { mode: 'cors' });
            const data2 = await response2.json();
            matches = data2.data;
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }

    async function fetchMatchWithPuuid(puuid) {
        try {
            const response2 = await fetch(`http://localhost:8088/api/matches?puuid=${puuid}`, { mode: 'cors' });
            const data2 = await response2.json();
            matches = data2.data;
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }



</script>

<main>
    {#if info.name}
        <h1>{info.name}</h1>
        <div class="container main-inner">
            <SummonerInfo {info}/>
            {#if matches}
                <SummonerMatches {matches}{pu}/>
            {/if}
        </div>
    {:else}
        <p>Loading...</p>
    {/if}
</main>
