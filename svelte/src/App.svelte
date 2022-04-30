<script>
	import Treemap from "./Treemap.svelte";
	import flare from "./flare-2.json";

	let width = 400;
	let height = 300;
</script>

<main bind:clientWidth={width} bind:clientHeight={height}>
	<Treemap data={flare} {width} {height} />
</main>

<style>
	main {
		width: 100%;
		height: 100%;
	}
</style>