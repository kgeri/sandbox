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
        console.log(node);
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
    const extents = tweened(undefined, {
        easing: cubicOut,
        duration: 500,
    });

    let selected = root;

    $: $extents = {
        x: selected.x0,
        y: selected.y0,
        width: selected.x1 - selected.x0,
        height: selected.y1 - selected.y0,
    };
</script>

<svg
    {width}
    {height}
    viewBox="{$extents.x} {$extents.y} {$extents.width} {$extents.height}"
    preserveAspectRatio="none"
>
    <TreeNode node={root} {selected} {onSelectNode} />
</svg>
