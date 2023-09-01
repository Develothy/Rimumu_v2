<script lang="ts">
    import { page } from '$app/stores';
    import SummonerInfo from './SummonerInfo.svelte';

    let info: any = {};
    let matches: any = {};

    console.log("tlqkf")

    let smn = $page.url.searchParams.get('smn');
    const encodedSmn = encodeURIComponent(smn);
    let userPuuid;

    fetch(`http://localhost:8088/api/summoner?smn=${encodedSmn}`, { mode: 'cors' })
        .then(response => response.json())
        .then(data => {
            info = data;
            userPuuid = info.puuid;

            // 첫 번째 fetch 완료 후, 두 번째 fetch 실행
            return fetch(`http://localhost:8088/api/summoner?smn=${userPuuid}`, { mode: 'cors' });
        })
        .then(response => response.json())
        .then(data => {
            console.log(userPuuid);
            matches = data;
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });



</script>

<main>
    {#if info.name}
        <h1>{info.name}</h1>
        <SummonerInfo {info} />
        <!-- 소환사 정보를 이용한 나머지 UI를 여기에 작성 -->
    {:else}
        <p>Loading...</p>
    {/if}
</main>
