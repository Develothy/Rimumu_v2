
<script>
    import { goto } from '$app/navigation';

    let smn = '';

    async function handleSubmit(event) {
        event.preventDefault();
        const response = await fetch('/api/summoner', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ smn }),
        });
        const data = await response.json();
        console.log(data);
        goto(`/summoner?data=${encodeURIComponent(JSON.stringify(data))}`);
    }
</script>

<div class="submit-form">
    <form class="form-subscribe" on:submit={handleSubmit} id="summoner" >
        <div class="row">
            <div class="col">
                <input type="text" bind:value={smn} class="form-control form-control-lg" id="smn" name="smn" placeholder="림우짱">
            </div>
            <div class="col-auto"><button type="submit" class="btn btn-primary btn-lg disabled" id="submitButton">검색</button>
            </div>
        </div>
    </form>
</div>