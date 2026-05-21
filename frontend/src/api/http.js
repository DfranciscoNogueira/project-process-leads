export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export async function getJson(path) {
    const response = await fetch(`${API_URL}${path}`);
    if (!response.ok) throw new Error(await response.text());
    return response.json();
}

export async function uploadCsv(file) {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch(`${API_URL}/api/lotes`, {method: 'POST', body: formData});
    if (!response.ok) throw new Error(await response.text());
    return response.json();
}
