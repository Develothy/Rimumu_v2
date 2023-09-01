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
