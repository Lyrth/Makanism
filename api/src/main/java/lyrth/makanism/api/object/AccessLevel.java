package lyrth.makanism.api.object;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public enum AccessLevel {  // TODO roles and more custom

    // Bot Owner
    OWNER(true),

    // Guild owner/Administrator (perm)
    ADMINISTRATOR(Permission.ADMINISTRATOR),

    // Manage Server (perm)
    MODERATOR(Permission.MANAGE_GUILD),

    // Everyone
    GENERAL(),

    // Custom perms
    CUSTOM()
    ;

    private static final Logger log = LoggerFactory.getLogger(AccessLevel.class);

    private static Snowflake ownerId = Snowflake.of(0L);

    private boolean botOwnerOnly;   // default true
    private boolean any = true;  // any permission or all perms
    private final HashSet<Permission> permissions = new LinkedHashSet<>();

    AccessLevel(boolean botOwnerOnly){
        this.botOwnerOnly = botOwnerOnly;
    }

    AccessLevel(Permission... perms){
        permissions.addAll(Arrays.asList(perms));
        botOwnerOnly = false;
    }

    AccessLevel(boolean any, Permission... perms){
        permissions.addAll(Arrays.asList(perms));
        this.any = any;
        botOwnerOnly = false;
    }

    public static AccessLevel custom(Permission... perms){
        AccessLevel accessLevel = AccessLevel.CUSTOM;
        accessLevel.permissions.addAll(Arrays.asList(perms));
        accessLevel.any = true;
        accessLevel.botOwnerOnly = false;
        return accessLevel;
    }

    public static AccessLevel custom(boolean any, Permission... perms){
        AccessLevel accessLevel = AccessLevel.CUSTOM;
        accessLevel.permissions.addAll(Arrays.asList(perms));
        accessLevel.any = any;
        accessLevel.botOwnerOnly = false;
        return accessLevel;
    }

    public static void setBotOwnerId(Snowflake botOwnerId){
        ownerId = Snowflake.of(botOwnerId.asLong());    // create a copy
    }

    public boolean isBotOwnerOnly() {
        return botOwnerOnly;
    }

    public boolean isAny() {
        return any;
    }

    public HashSet<Permission> getPermissions() {
        return permissions;
    }

    public PermissionSet getPermissionSet(){
        return PermissionSet.of(permissions.toArray(new Permission[]{}));
    }

    public Mono<Boolean> allows(@Nullable Member member){
        if (member == null) return Mono.empty();
        if (botOwnerOnly)
            return Mono.just(member.getId().equals(ownerId)); //TODO: get ID from storage
        if (permissions.isEmpty())
            return Mono.just(true);
        return member.getBasePermissions()
            .flatMapIterable(PermissionSet::asEnumSet)
            .filter(permissions::contains)
            .count()
            .map(count -> any ? count >= 1 : count == permissions.size())
            .defaultIfEmpty(false);
    }

    public Mono<Boolean> allows(@Nullable User user){
        if (user == null) return Mono.empty();
        if (botOwnerOnly)
            return Mono.just(user.getId().equals(ownerId)); //TODO: get ID from storage
        return Mono.just(permissions.isEmpty());
    }
}
