<script lang="ts">
    import { page } from '$app/stores';
    import SummonerInfo from './SummonerInfo.svelte';
    import SummonerMatches from "./SummonerMatches.svelte";

    let info: any = {};
    let matches: any = [];

    console.log("tlqkf")

    let smn = $page.url.searchParams.get('smn');
    const encodedSmn = encodeURIComponent(smn);
    let userPuuid;

    // 데이터 로딩 함수 정의
    async function fetchData() {
        try {
            const response1 = await fetch(`http://localhost:8088/api/summoner?smn=${encodedSmn}`, { mode: 'cors' });
            const data1 = await response1.json();
            info = data1.data;
            userPuuid = info.puuid;
            console.log(info)

            const response2 = await fetch(`http://localhost:8088/api/matches?userPuuid=${userPuuid}`, { mode: 'cors' });
            const data2 = await response2.json();
            matches = data2.data;
            console.log(matches)
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }

    // 데이터 로딩 함수 호출
    fetchData();

</script>

<main>
    {#if info.name}
        <h1>{info.name}</h1>
        <div class="container main-inner">
            <SummonerInfo {info}/>
            {#if matches}
                <SummonerMatches {matches}/>
            {/if}
        </div>
    {:else}
        <p>Loading...</p>
    {/if}
</main>
