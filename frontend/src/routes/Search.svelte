
<script>
    import { goto } from '$app/navigation';

    let smn = '';

    async function handleSubmit(event) {
        event.preventDefault();

        await goto(`/summoner?smn=${encodeURIComponent(smn)}`);
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