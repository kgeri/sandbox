<script>
    import TreeNode from "./TreeNode.svelte";
    import { tweened } from "svelte/motion";
    import { cubicOut } from "svelte/easing";
    import * as d3 from "d3";

    export let data;
    export let width;
    export let height;

    function onSelectNode(node) {
        selected = node;
    }

    function tile(node, x0, y0, x1, y1) {
        d3.treemapBinary(node, 0, 0, width, height);
        for (const child of node.children) {
            child.x0 = x0 + (child.x0 / width) * (x1 - x0);
            child.x1 = x0 + (child.x1 / width) * (x1 - x0);
            child.y0 = y0 + (child.y0 / height) * (y1 - y0);
            child.y1 = y0 + (child.y1 / height) * (y1 - y0);
        }
    }

    const tm = (data) =>
        d3.treemap().tile(tile)(
            d3
                .hierarchy(data)
                .sum((d) => d.value)
                .sort((a, b) => b.value - a.value)
        );

    const root = tm(data);
    let selected = root;

    const translation = tweened(undefined, {
        easing: cubicOut,
        duration: 500,
    });

    $: $translation = {
        tx: root.x0 - selected.x0,
        ty: root.y0 - selected.y0,
        sw: (root.x1 - root.x0) / (selected.x1 - selected.x0),
        sh: (root.y1 - root.y0) / (selected.y1 - selected.y0),
        width: width,
        height: height,
    };
</script>

<div
    style="width: {width}px; height: {height}px; position: relative; overflow: hidden;"
>
    <TreeNode
        node={root}
        translation={$translation}
        {selected}
        {onSelectNode}
    />
</div>
