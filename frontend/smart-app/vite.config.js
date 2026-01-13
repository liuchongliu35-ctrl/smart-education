import { defineConfig } from "vite";
   import react from "@vitejs/plugin-react";
   import path from "path";
   import markdown from 'vite-plugin-markdown';

   export default defineConfig({
    //  assetsInclude: ['**/*.md'],
     plugins: [
      react(),
      // markdown() // 添加插件
     ],
     resolve: {
       alias: {
         "@": path.resolve(__dirname, "./src"),
       },
     },
   });


