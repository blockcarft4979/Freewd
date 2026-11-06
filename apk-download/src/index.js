export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const match = url.pathname.match(/^\/apk\/(latest|[0-9A-Za-z._-]+)\.apk$/);
    if (!match) return new Response('APK Download Service', { status: 200 });

    if (request.method !== 'GET' && request.method !== 'HEAD') {
      return new Response('Method Not Allowed', { status: 405, headers: { Allow: 'GET, HEAD' } });
    }

    const name = match[1];
    const owner = 'blockcarft4979';
    const repo = 'Freewd';
    const assetName = 'freewd-release.apk'; // 确保和 Action 里重命名的名字一致

    const githubUrl = name === 'latest'
      ? `https://github.com/${owner}/${repo}/releases/latest/download/${assetName}`
      : `https://github.com/${owner}/${repo}/releases/download/v${name}/${assetName}`;

    // 不要直接透传 request.headers，只保留支持断点续传的 Range 头
    const fetchHeaders = new Headers();
    if (request.headers.has('range')) {
      fetchHeaders.set('range', request.headers.get('range'));
    }

    const response = await fetch(githubUrl, {
      method: request.method,
      headers: fetchHeaders,
      redirect: 'follow', // 跟随 GitHub 的 302 重定向
    });

    if (!response.ok) {
      return new Response(`APK not found (GitHub 返回 ${response.status})`, { status: response.status });
    }

    const headers = new Headers(response.headers);
    headers.set('Content-Type', 'application/vnd.android.package-archive');
    headers.set('Content-Disposition', `attachment; filename="freewd-${name}.apk"`);
    headers.set('Cache-Control', name === 'latest' ? 'public, max-age=60' : 'public, max-age=31536000, immutable');
    
    return new Response(response.body, {
      status: response.status,
      headers,
    });
  },
};