import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:8088',
				changeOrigin: true,
				headers: {
					'Access-Control-Allow-Origin': 'http://localhost:5173'
				}
			}
		}
	}
});
