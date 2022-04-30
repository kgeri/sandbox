<script>
    export let node;
    export let translation;
    export let selected;
    export let onSelectNode;

    function isVisible(node, selected) {
        let s = selected;
        while (s != null) {
            if (node.parent === s) return true;
            s = s.parent;
        }
        return false;
    }
</script>

{#if isVisible(node, selected)}
    <div
        class="node"
        class:leaf={!node.children}
        style="left: {(node.x0 + translation.tx) *
            translation.sw *
            translation.width}px; top: {(node.y0 + translation.ty) *
            translation.sh *
            translation.height}px; width: {(node.x1 - node.x0) *
            translation.sw *
            translation.width}px; height: {(node.y1 - node.y0) *
            translation.sh *
            translation.height}px;"
        on:click={() => onSelectNode && node.children && onSelectNode(node)}
    >
        <strong>{node.data.name}</strong>
    </div>
{/if}
{#each node.children || [] as n}
    <svelte:self node={n} {translation} {selected} {onSelectNode} />
{/each}

<style>
    .node {
        position: absolute;
        background-color: gray;
        border: 1px solid black;
        overflow: hidden;
    }
    .node:not(.leaf) {
        cursor: pointer;
    }
    .node:hover {
        background-color: red;
    }
    .leaf {
        background-color: silver;
    }
</style>
