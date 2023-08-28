<script lang="ts">
    import { page } from '$app/stores';

    let data: any = {};

    let smn = $page.url.searchParams.get('smn')

    if (smn) {
        const encodedSmn = encodeURIComponent(smn);
        fetch(`http://localhost:8088/api/summoner?smn=${encodedSmn}`,
            {mode: 'cors'})
            .then(response => response.json())
            .then(result => {
                data = result;
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
    }
</script>

{#if data.name}
    <h1>{data.name}</h1>
    <p>{smn}</p>
    <p>{data.name}</p>
    <!-- 데이터를 화면에 표시 -->
{:else}
    <p>Loading...</p>
{/if}


