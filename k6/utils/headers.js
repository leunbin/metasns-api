//jwt
export function jsonHeaders(token){
    return {
        headers: {
            "Content-Type": "application/json",
            Authorization: token ? `Bearer ${token}` : undefined,
        },
    };
}