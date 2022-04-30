<script>
    export let node = null;
    export let selected = null;
    export let onSelectNode;

    function isVisible(node, selected) {
        let s = selected;
        while (s != null) {
            if (node === s) return true;
            s = s.parent;
        }
        return false;
    }
</script>

{#if node && isVisible(node, selected)}
    {#each node.children || [] as n}
        <rect
            class="node"
            x={n.x0}
            y={n.y0}
            width={n.x1 - n.x0}
            height={n.y1 - n.y0}
            vector-effect="non-scaling-stroke"
            on:click={() => onSelectNode && onSelectNode(n)}
        />
        <svelte:self node={n} {selected} {onSelectNode} />
    {/each}
{/if}

<style>
    .node {
        fill: gray;
        stroke: black;
        stroke-width: 1;
        cursor: pointer;
    }
    .node:hover {
        fill: red;
    }
</style>
