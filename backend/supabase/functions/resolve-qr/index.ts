// App-QrCodeYar v2.0 - public Dynamic QR resolver.

declare const EdgeRuntime: { waitUntil(promise: Promise<unknown>): void };
//
// Printed QR codes must be resolvable without a caller JWT, therefore this function is deployed
// with verify_jwt=false. The backend secret is read only inside the Edge Function runtime and is
// never embedded in the Android APK. The implementation uses the Data API directly so the function
// has no third-party runtime dependency to drift between releases.

function serverSecret(): string {
  const modern = Deno.env.get("SUPABASE_SECRET_KEYS") ?? "";
  if (modern) {
    try {
      const keys = JSON.parse(modern) as Record<string, string>;
      if (keys.default) return keys.default;
    } catch {
      // Fall through to the legacy environment variable for older hosted projects.
    }
  }
  return Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
}

async function dataApi(path: string, secret: string, init: RequestInit = {}): Promise<Response> {
  const base = Deno.env.get("SUPABASE_URL") ?? "";
  if (!base) return new Response("Backend is not configured", { status: 503 });
  const headers = new Headers(init.headers);
  headers.set("apikey", secret);
  headers.set("Authorization", `Bearer ${secret}`);
  headers.set("Accept", "application/json");
  if (init.body) headers.set("Content-Type", "application/json");
  return fetch(`${base}${path}`, { ...init, headers });
}

export default {
  fetch: async (req: Request): Promise<Response> => {
    if (req.method !== "GET" && req.method !== "HEAD") {
      return new Response("Method not allowed", { status: 405, headers: { Allow: "GET, HEAD" } });
    }

    const url = new URL(req.url);
    const slug = (url.searchParams.get("slug") ?? "").trim().toLowerCase();
    if (!/^[a-z0-9][a-z0-9_-]{2,39}$/.test(slug)) {
      return new Response("Invalid QR slug", { status: 400 });
    }

    const secret = serverSecret();
    if (!secret) return new Response("Backend is not configured", { status: 503 });

    const lookup = await dataApi(
      `/rest/v1/dynamic_qr?slug=eq.${encodeURIComponent(slug)}&enabled=eq.true&select=id,destination_url&limit=1`,
      secret,
    );
    if (!lookup.ok) return new Response("QR lookup failed", { status: 502 });
    const rows = await lookup.json() as Array<{ id?: string; destination_url?: string }>;
    const qr = rows[0];
    if (!qr?.id || !qr.destination_url) return new Response("QR not found", { status: 404 });

    // Analytics is intentionally best-effort: a metrics failure must never break a printed QR.
    if (req.method === "GET") {
      const country = (req.headers.get("cf-ipcountry") ?? "").slice(0, 8);
      const city = (req.headers.get("cf-ipcity") ?? "").slice(0, 80);
      const eventBody = JSON.stringify({ dynamic_qr_id: qr.id, country_code: country, city });
      EdgeRuntime.waitUntil(
        dataApi("/rest/v1/qr_scan_events", secret, {
          method: "POST",
          headers: { Prefer: "return=minimal" },
          body: eventBody,
        }).then(() => undefined).catch(() => undefined),
      );
    }

    return Response.redirect(qr.destination_url, 302);
  },
};
