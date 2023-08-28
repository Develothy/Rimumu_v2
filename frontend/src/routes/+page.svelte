<script>
	import Search from './Search.svelte';
	import rimumugg from '$lib/images/rimumugg.png';

</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="Svelte demo app" />
</svelte:head>


<div class="container position-relative">
	<div class="row justify-content-center">
		<div class="col-xl-6">
			<div class="text-center text-white">
				<img src={rimumugg} width="500px" class="mb-5" id="margin-top" alt="URIM.GG" >
				<Search />
			</div>
		</div>
	</div>
</div>

<style>
	section {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		flex: 0.6;
	}

	h1 {
		width: 100%;
	}

	.welcome {
		display: block;
		position: relative;
		width: 100%;
		height: 0;
		padding: 0 0 calc(100% * 495 / 2048) 0;
	}

	.welcome img {
		position: absolute;
		width: 100%;
		height: 100%;
		top: 0;
		display: block;
	}
</style>
